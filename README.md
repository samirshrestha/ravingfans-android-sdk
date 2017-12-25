# ravingfans-android-sdk
Android SDK for RavingFans™ Platform

## Changes

* Endpoint upgraded to HTTPS, data transport is now encrypted.
* Code is now open source under MIT license, feel free to fork and PR.

## Integration

* Download or clone the `RFAnalytics.java` file, Drop it into your project’s src/main/java folder.

* On your main activity’s onStart() method, add these two lines of code -
```java
RFAnalytics.init(context, “YOUR_KEY”);
RFAnalytics.startSession();
```
* On your main activity’s onStop() method, add this line of code -
```java
RFAnalytics.stopSession();
```
That's it.

## License

RavingFansSDK is available under the MIT license. See the LICENSE file for more info.
