package br.com.argmax.imagelabeling.application.imageclassification

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil.inflate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.argmax.imagelabeling.R
import br.com.argmax.imagelabeling.application.imageclassification.ImageClassificationViewModel.ImageClassificationViewModelState
import br.com.argmax.imagelabeling.databinding.FragmentImageClassificationBinding
import br.com.argmax.imagelabeling.service.entities.imageclass.ImageClassResponseDto
import br.com.argmax.imagelabeling.service.entities.rapidapientities.RapidApiImageResponseDto
import br.com.argmax.imagelabeling.utils.ViewModelFactoryProvider
import com.bumptech.glide.Glide
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ImageClassificationFragment : DaggerFragment() {

    @Inject
    lateinit var mViewModelFactoryProvider: ViewModelFactoryProvider

    private var mViewModel: ImageClassificationViewModel? = null

    private val args: ImageClassificationFragmentArgs by navArgs()

    private var mImageClassResponseDto: ImageClassResponseDto? = null
    private var mBinding: FragmentImageClassificationBinding? = null

    private var mImageResponseDtoList = mutableListOf<RapidApiImageResponseDto>()

    private var mSearchTerm: String? = null
    private var mListPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding = inflate(inflater, R.layout.fragment_image_classification, container, false)

        unwrapArgs()
        initViewModel()

        return mBinding?.root
    }

    private fun unwrapArgs() {
        mImageClassResponseDto = args.imageCLassResponseDto
    }

    private fun initViewModel() {
        mViewModel = ViewModelProvider(this, mViewModelFactoryProvider)
            .get(ImageClassificationViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupViewModel()
        setupInteractions()
    }

    private fun setupView() {
        mImageClassResponseDto?.let {
            setImageClassDataIntoView(it)
        }

        setupButtons()
    }

    private fun setImageClassDataIntoView(imageClassResponseDto: ImageClassResponseDto) {
        val imageClassId = imageClassResponseDto.id.toString()
        val imageClassName = imageClassResponseDto.name

        mBinding?.toolbarTitle?.text = imageClassName
        mBinding?.imageClassIdTextView?.text = imageClassId
        mBinding?.imageClassNameTextView?.text = imageClassName
    }

    private fun setupViewModel() {
        mViewModel?.getStateLiveData()?.removeObservers(viewLifecycleOwner)

        mViewModel?.getStateLiveData()?.observe(
            viewLifecycleOwner,
            Observer { viewModelState ->
                handleViewModelState(viewModelState)
            })
    }

    private fun handleViewModelState(viewModelState: ImageClassificationViewModelState) {
        when (viewModelState) {
            is ImageClassificationViewModelState.Loading -> {
                mBinding?.contentLoadingProgressBar?.visibility = View.VISIBLE
            }

            is ImageClassificationViewModelState.Error -> {
                hideProgressBar()
                print(viewModelState.throwable.localizedMessage)
            }

            is ImageClassificationViewModelState.GetRapidImageSuccess -> {
                hideProgressBar()
                changeSearchTermViewVisibility()

                viewModelState.data?.let {
                    mImageResponseDtoList.addAll(it)
                }

                updateImageView()
            }

            is ImageClassificationViewModelState.SendImageSuccess -> {
                hideProgressBar()
                incrementPosition()
                updateImageView()
            }
        }
    }

    private fun updateImageView() {
        mBinding?.imageView?.let { imageView ->
            context?.let { contextUnShadowed ->
                Glide.with(contextUnShadowed)
                    .load(mImageResponseDtoList[mListPosition].url)
                    .into(imageView)
            }
        }
    }

    private fun hideProgressBar() {
        mBinding?.contentLoadingProgressBar?.visibility = View.GONE
    }

    private fun setupInteractions() {
        setupToolbarBackNavigation()
        setupSearchButtonClick()
    }

    private fun setupToolbarBackNavigation() {
        mBinding?.toolbarBackIcon?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSearchButtonClick() {
        mBinding?.searchTermSearchIcon?.setOnClickListener {
            mBinding?.searchTermEditText?.text.toString().let { searchTerm ->
                mViewModel?.getRapidImage(searchTerm)
                hideKeyboard()
            }
        }

        mBinding?.searchTermEditText?.setOnEditorActionListener(
            OnEditorActionListener { textView, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val searchTerm = textView.text.toString()

                    mViewModel?.getRapidImage(searchTerm)

                    hideKeyboard()
                    return@OnEditorActionListener true
                }
                false
            }
        )
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        val flags = 0

        inputMethodManager?.hideSoftInputFromWindow(view?.windowToken, flags)
    }

    private fun changeSearchTermViewVisibility() {
        if (mBinding?.searchTermDefaultView?.visibility == View.GONE) {
            mSearchTerm = mBinding?.searchTermEditText?.text.toString()
            mBinding?.searchTermEditView?.visibility = View.GONE

            mBinding?.searchTermTextView?.text = mSearchTerm
            mBinding?.searchTermDefaultView?.visibility = View.VISIBLE
        } else {
            mBinding?.searchTermDefaultView?.visibility = View.GONE
            mBinding?.searchTermEditView?.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        mBinding?.discardButton?.setText(getString(R.string.image_classification_fragment_discard_button_label))
        mBinding?.discardButton?.isConfirmationButton(false)
        mBinding?.discardButton?.setOnClickListener {
            showNextImage()
        }

        mBinding?.confirmButton?.setText(getString(R.string.image_classification_fragment_confirm_button_label))
        mBinding?.discardButton?.isConfirmationButton(true)
        mBinding?.confirmButton?.setOnClickListener {
            confirmImageClassification()
            showNextImage()
        }
    }

    private fun showNextImage() {
        incrementPosition()

        val threshold = 10
        if (mListPosition == mImageResponseDtoList.size - threshold) {
            mSearchTerm?.let {
                mViewModel?.getRapidImage(searchTerm = it)
            }
        } else {
            updateImageView()
        }
    }

    private fun confirmImageClassification() {
        mImageClassResponseDto?.let { imageClassResponseDto ->
            mViewModel?.confirmImageClassification(
                mImageResponseDtoList[mListPosition],
                imageClassResponseDto
            )
        }
    }

    private fun incrementPosition() {
        mListPosition += 1
    }
}
