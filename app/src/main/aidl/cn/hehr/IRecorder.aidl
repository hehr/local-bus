// IRecorder.aidl
package cn.hehr;

// Declare any non-default types here with import statements
import cn.hehr.ICallback;

interface IRecorder {

   void start(ICallback call);

   void stop();

}
