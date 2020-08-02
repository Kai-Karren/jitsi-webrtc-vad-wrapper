### Jitsi-WebRTCVad-Wrapper Fork
#### by Kai Karren

This repository is based on https://github.com/jitsi/jitsi-webrtc-vad-wrapper.
With the following modifications:
- Simplified the use of pcm audio as byte[] with the WebRTCVad class.
- Modified to pom file to build a fat jar with all required dependencies. This fat jar allows to use the WebRTCVad-Wrapper easily in your projects.

To build the fat jar simply execute `mvn install` and add the created fat jar (jar-with-dependencies)
to your project. You have **not** to compile libfvad.so or webrtcwrapper.so by yourself. However, you can if you want to.

### Using the Wrapper with PCM audio

New simplified version provided by this fork to enable a simple, consistent interface similar to other WebRTCVad wrappers or VAD implementations.
```java

byte[] audio = loadAudioAsByteArray("fileName.wav");

byte[] frame = getFrameFromAudio(audio, 0);

/*
 * The voice activity needs 2 parameters, sample rate and operating mode.
 *
 * Supported sample rates are 8, 16, 32 and 48 KHz.
 *
 * Valid modes are 0 ("quality"), 1 ("low bitrate"), 2 ("aggressive"), and 3
 * ("very aggressive").  The more aggressive the mode, the higher the
 * probability that active speech is detected, which also increases the
 * amount of false positives.
 */
WebRTCVad webRTCVad = new WebRTCVad(16000, 0);

System.out.println(webRTCVad.isSpeech(frame))

```


Because the official repo is king of saving in terms of documentation, I wanted to point out
that they already included the conversion from byte[] to and int[] which simplifies
the usage of PCM audio e.g from a microphone, or a WAV file. 

```java

byte[] audio = loadAudioAsByteArray("fileName.wav");
byte[] frame = getFrameFromAudio(audio, 0);


WebRTCVad webRTCVad = new WebRTCVad(16000, 0);

ByteSignedPcmAudioSegment audioSegment = new ByteSignedPcmAudioSegment(frame);

System.out.println(webRTCVad.isSpeech(audioSegment.to16bitPCM()));
```

Thanks again for sharing this wrapper for Java!

The original readme is provided below.

### Original Readme

This repository contains a java wrapper around the native VAD engine that is
part of the WebRTC native code package (https://webrtc.org/native-code/), 
as provided in a fork at https://github.com/dpirch/libfvad.

### Building

In order for the maven package to function, two native shared libraries are
required. The first library, `libfvad.so`, is the shared library of the VAD 
engine. This library can be compiled on Ubuntu 18.04 by running the script  
`compile_libfvad.sh`. See https://github.com/dpirch/libfvad for more details.

The second library, `webrtcvadwrapper.so`, is JNI c++ code wrapping around
`libfvad.so`. The headers can be (re)generated using `generate_jni_headers.sh` 
and the library can be compiled using `compile_libwebrtcvadwrapper.sh`.

To install locally, simply run `mvn install`. As for now, a folder containing 
the shared library files needs to be manually added to the `java.library.path` 
property of any application using this maven module. This can be done by
for example setting the environment variable `LD_LIBRARY_PATH` to 
`/path/to/shared/libraries:$LD_LIBRARY_PATH`.

### Using the wrapper


The VAD engine requires mono, 16-bit PCM audio with a sample rate of 8, 16, 32
or 48 KHz as input. The input should be an audio segment of 10, 20 or 30 
milliseconds. When the audio input is 16 Khz, the input array should thus be 
either of length 160, 320 or 480. 

The voice activity detection can run in 4 different modes. The modes range from 
0 to 3. Mode 0 is very strict, which means the probability of the audio segment
being speech when the VAD predicts it is speech is higher. Mode 3 is very
aggressive, which means the probability of the audio being speech when the
VAD predicts it is is lower. As expected, mode 1 and 2 gradually decrease
this probability.

The example below shows the creation of a `WebRTCVad` object which accepts
16 Khz audio and is running in mode 1.

```java
import org.jitsi.webrtcvadwrapper.WebRTCVad;

class Example
{
    public static void main(String[] args)
    {
        int[] linear16Audio = new int[] { /* 160, 320 or 480 integer values */ };
        
        WebRTCVad vad = new WebRTCVad(16000, 1);
        boolean isSpeechSegment = vad.isSpeech(linear16Audio);
    }    
}
```

### Credit

Thanks to Daniel Pirch and the WebRTC project authors for providing the
VAD Engine. 

