# SortPhotosRecognizePlantsPutToMySQL

That's JavaFX application, which tasks are:

**I. Manage photos (ManageIncomingPhotos.java)**
1. Consider the directory with plant's images(jpeg-files), select from them ones with suitable dates and geolocations.
2. Recognize plant with PlantNet API
3. Organize directory plant's images, have been recognized, put records to MySQL with plant, date, geolocation and path to image-file.

**II. Manage database (GroupOpAndDataBaseCorrections.java)**
1. Put in the database records as 'one packet', if you don't need to recognize every image.
2. Correct geolocation of plant-points before and after putting it to the database.
3. Find and refresh wiki-pages for plants in different languages.

To recognize plant online you need PlantNet api-key, it's free: https://plantnet.org/en/.
Put api-key in the field of class CommonConstants.
You can recognize 30 plants every day free of charge.

Default values for longitude and latitude in CommonConstants.class are given for WestPark, Munich.






