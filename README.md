# NITKart
## Shopping application on Android
<br><br>
This is a pure Android application. <br>
It has the following features.
* Firebase based Authentication. <br>
* Displays the list of products as available in the Firebase database. (Check for the sample 
    [here](https://github.com/akshayub/NITKart/blob/master/fireBaseServerDetails/sampleJsonDatabase.json)) <br>
* Has a shopping cart.
<br>
<hr>

Check [manifest](https://github.com/akshayub/NITKart/blob/master/app/src/main/AndroidManifest.xml) for further technical details. <br><br>
The sample google-services json template is [here](https://github.com/akshayub/NITKart/blob/master/app/google-services.json)  

<hr>

The APK Folder contains a sample APK, but it will not display any images next to the products 
because the server.py Imageserver should be running. <br>
The imager server is thanks to the [gist](https://gist.github.com/peterkuma/8916745)

<br><br>

### NOTE 
* The **images should be named with the corresponding product ID**, and **jpg format** is preferred. <br>
* You need to register the application imported to Android studio with Firebase.<br>

<hr>

## Contributing
<br>
Feel free to add more features, like search functionality, and sorting/filtering the items, categorizing them into different categories based on the type, etc ...
