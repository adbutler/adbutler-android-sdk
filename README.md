# adbutler-android-sdk

## Installation

### JitPack

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

And then, add the dependency

```groovy
dependencies {
	compile 'com.github.sparklit:adbutler-android-sdk:1.0'
}
```

## Usage

Please refer to https://adbutler.com/docs/api for up-to-date API documentation.

### Requesting Single Placement

To request a placement, you can build an instance of `PlacementRequestConfig` and specify the attributes you want to send:

```java
PlacementRequestConfig config = new PlacementRequestConfig.Builder(153105, 214764, 300, 250).build();
AdButler adbutler = new AdButler();
adbutler.requestPlacement(config, new PlacementResponseListener() {
  // handle response
});
```

### Requesting Multiple Placements

To request multiple placements, you need a list of `PlacementRequestConfig`s, and each for a placement respectively:

```java
List<PlacementRequestConfig> configs = getPlacementRequestConfigs();
AdButler adbutler = new AdButler();
adbutler.requestPlacements(configs, new PlacementResponseListener() {
  // handle response
});
```

### Handling the Response

Placement(s) request will take a given listener, and call its methods based on the status of the response accordingly.

```java
AdButler adbutler;
adbutler.requestPlacements(configs, new PlacementResponseListener() {
  @Override
  public void success(PlacementResponse response) {
    // Handle success case
  }

  @Override
  public void error(Throwable throwable) {
    // Handle error cases
  }
});
```

### Request Pixel

You can request a pixel simply by giving the URL:

```java
AdButler adbutler = new AdButler();
adbutler.requestPixel("https://servedbyadbutler.com/default_banner.gif");
```

### Record Impression

When you have a `Placement`, you can record impression by:

```java
Placement placement;
placement.recordImpression()
```

### Record Click

Similarly, you can record click for a `Placement`:

```java
Placement placement;
placement.recordClick()
```

### Best Practice for Recording

The best practice is to record impression at the time when placement is actually visible on the screen; and record click when it is actually tapped.

## Sample Project

Please check out the `sample` project inside this repository to see more sample code about how to use this SDK.

# License

This SDK is released under the Apache 2.0 license. See [LICENSE](https://github.com/sparklit/adbutler-android-sdk/tree/master/LICENSE) for more information.
