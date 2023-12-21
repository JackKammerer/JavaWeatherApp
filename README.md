# Installation

The prebuilt .jar archive is located in *"target/"* and can be ran. Make sure a *.env* file is included in the same directory as the jar, and it has values for the Visual Crossing Weather API key, the Discord API key, the UserID of the Discord Account to receive the weather message, and a location string. Consult the WeatherApp.java file (under the source tree), to determine how to label these values. 

**Note:** The location string should be written as defined by the Visual Crossing Weather API documentation. There are a wide variety of options. The easiest is to do a "<City>,<State>,<Nation>" format, without any spaces included. The API is relative versatile at figuring out what the query means, just ensure there are no spaces as this is directly injected into the URL. 
