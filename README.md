# Viewcue
Udacity Android Developer Movie Stage 2 App

![Loading Images in ViewQ](http://s696797961.websitehome.co.uk/lundie/ghub_gif/lundie_viewCue_imageLoad3.gif)

# Editing the API Key
This application requires the use of an API key from [The Movie Database](https://www.themoviedb.org/). Please add your own API key in your local gradle.properties file located in the application root.

# Project Notes
A first attempt at several things, most noteably in using injection, LiveData and following MVVM app architecture - biting of far more than one could chew in the process - but eventually learning a lot. Incredibly enjoyable and challenging process. 

# Notable
Offline access gained through the combination of a ROOM database (initially using 'one true source' methodology) and utilising Picasso image caching features.

# Future Intentions
I intend to return to this project to more fully utilise Dagger injection.

# References

Below are some of the references used while producing this application. Other references are noted directly within comments in the code.

MVVM architecture:

https://proandroiddev.com/mvvm-architecture-viewmodel-and-livedata-part-1-604f50cda1

https://medium.com/corebuild-software/simple-android-mvvm-using-rx-and-kotlin-9769a91b03ef

https://github.com/hazems/mvvm-sample-app/blob/part1

Retrofit:

https://www.simplifiedcoding.net/android-viewmodel-using-retrofit/

https://guides.codepath.com/android/consuming-apis-with-retrofit

https://futurestud.io/tutorials/retrofit-2-how-to-add-query-parameters-to-every-request

https://inthecheesefactory.com/blog/retrofit-2.0/en
