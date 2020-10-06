package br.com.argmax.imagelabeling.service.remote.domain

import br.com.argmax.imagelabeling.service.entities.domain.DomainResponseDto
import br.com.argmax.imagelabeling.service.entities.domain.DomainRequestDto

class DomainRemoteDataSourceImpl(
    private val mDomainApiDataSource: DomainApiDataSource
) : DomainRemoteDataSource {

    override suspend fun domainList(): List<DomainResponseDto> {
        return mDomainApiDataSource.domainList()
    }

    override suspend fun createDomain(domainRequestDto: DomainRequestDto): DomainResponseDto {
        return mDomainApiDataSource.createDomain(domainRequestDto)
    }

    override suspend fun editDomain(
        domainId: Int,
        domainRequestDto: DomainRequestDto
    ): DomainResponseDto {
        return mDomainApiDataSource.editDomain(
            domainId = domainId,
            domainRequestDto = domainRequestDto
        )
    }

}