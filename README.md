# [ARCHIVED] eConferencing Android

**⚠️ This project is no longer maintained and has been archived.**  
Please note that this repository is now in a read-only state and will not receive any further updates or support.

We recommend using to the latest version of the **Agora Android SDK**: [Learn more](https://www.agora.io/en/products/video-call/) 

For documentation and support, please visit the [Agora Documentation](https://docs.agora.io/en/).
 
 ---
 > *其他语言版本：[简体中文](README.zh.md)*

This page introduces how to run the Android sample project.

## Prerequisites 

- Make sure you have made the preparations mentioned in the [Agora Meeting Guide](https://github.com/AgoraIO-Usecase/AgoraMeeting/blob/master/README.md).
- Prepare the development environment:
  - JDK
  - Android Studio 3.6 or later
- Real Android devices, such as Nexus 5X. We recommend using real devices because some function may not work well on simulators or you may encounter performance issues.

## Run the sample project

Follow these steps to run the sample project:

**1.Clone the repository to your local machine.**

```
git clone https://github.com/AgoraIO-Usecase/AgoraMeeting.git
```

**2.Enter the directory of the Android project.**

```
cd AgoraMeeting_Android/
```

**3.Open the Android project with Android Studio.**

**4.Configure keys.**

Pass the following parameters in `app/src/main/res/values/string_configs.xml`:
- The Agora App ID that you get.
- The `Authorization`parameter that you have generated for basic HTTP authentication.

```
<string name="agora_app_id" translatable="false"><#YOUR APP ID#></string>
<string name="agora_auth" translatable="false"><#YOUR AUTH#></string>
```

For details, see the [prerequisites](https://github.com/AgoraIO-Usecase/AgoraMeeting/blob/master/README.md#prerequisites) in Agora E-education Guide.

**5.Run the project.**

## Connect us

- You can read the full set of documentations and API reference at [Agora Developer Portal](https://docs.agora.io/en/).
- You can ask for technical support by submitting tickets in [Agora Console](https://dashboard.agora.io/). 
- You can submit an [issue](https://github.com/AgoraIO-Usecase/AgoraMeeting/issues) if you find any bug in the sample project. 

## License

Distributed under the MIT License. See `LICENSE` for more information.
