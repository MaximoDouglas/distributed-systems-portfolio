## Image Labeling System

### Simplified architecture
![Image Labeling system simple diagram](image-labeling-diagrams.png)

### Usage description
- In the device the user fills the form for __imagem domain__ creation, e.g. 'cats and dogs';
- The device send the __image domain__ object to the __Ruby API__, which will use it to use to store related images;
- Once the __image domain__ is created, the user may see it on domains screen of the app;
- Accessing the __image domain__, the user can now create the classes for this domain, if there's not classes created for this domain;
- Once the user creates the classes, the device send it to the __Ruby API__, which will request image URLs for this classes from the __Google Images API__ and save them in the database;
- With the image URLs for the classes saved in the __Ruby API__, the user can now select a class to start the labeling proccess;
- If there's already image URLs in the __Ruby API__ for the user to classify, he will see the images in a screen with two buttons:
  - __Discard__: which will discard this image if it does not belong to the class the user is labeling;
  - __OK__: which confirms that this image belongs to the class being labeled.
- Once the user is done, he can simply press return. 