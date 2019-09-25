# StatusHandler

[ ![Download](https://api.bintray.com/packages/shostko/android/error/images/download.svg) ](https://bintray.com/shostko/android/error/_latestVersion)
Wraps and helps working with exceptions and errors

## Integration

As soon as it is still in development you should add to your project Gradle configuration:

```gradle
repositories {
    maven { url "https://dl.bintray.com/shostko/android" }
}
```

Base module integration:
```gradle
dependencies {
    implementation 'by.shostko:error:0.+'
}
```

For additional support of RxJava and StatusHandler add any of these:
```gradle
dependencies {
    implementation 'by.shostko:error-rx:0.+'
    implementation 'by.shostko:error-status:0.+'
    implementation 'by.shostko:error-status-viewmodel:0.+'
}
```

Also don't forget to additional mandatory dependencies:
```gradle
dependencies {
    // for rx module
    implementation 'io.reactivex.rxjava2:rxjava:2.+' 
    
    // for status module
    implementation 'by.shostko:status-handler:0.+'
    
    // for status-viewmodel module
    implementation 'by.shostko:status-handler:0.+'
    implementation 'by.shostko:status-handler-viewmodel:0.+'

    // plus dont forget to add required dependencies for status-handler library itself
}
```

### License

Released under the [Apache 2.0 license](LICENSE).

```
Copyright 2019 Sergey Shostko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
