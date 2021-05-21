# Errors

[![Maven Central](https://img.shields.io/maven-central/v/by.shostko/error?style=flat)](#integration) [![API-level](https://img.shields.io/badge/API-14+-blue?style=flat&logo=android)](https://source.android.com/setup/start/build-numbers) [![License](https://img.shields.io/badge/license-Apach%202.0-green?style=flat)](#license) 

Wraps and helps working with exceptions and errors

## Integration

The library is now available in Maven Central repository:

```gradle
dependencies {
    implementation 'by.shostko:error:0.+'
}
```

For additional support of RxJava, Worker and StatusHandler add any of these:
```gradle
dependencies {
    implementation 'by.shostko:error-rx:0.+'
    implementation 'by.shostko:error-rx-worker:0.+'
    implementation 'by.shostko:error-status:0.+'
    implementation 'by.shostko:error-status-viewmodel:0.+'
}
```

Also don't forget to additional mandatory dependencies:
```gradle
dependencies {
    // for rx module
    implementation 'io.reactivex.rxjava2:rxjava:2.+' 

    // for rx-worker module
    implementation 'android.arch.work:work-runtime-ktx:1.+'
    implementation 'android.arch.work:work-rxjava2:1.+'
    
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
