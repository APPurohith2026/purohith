package com.apbrahmanapurohithsamakya.app;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
 @Override public void onCreate(Bundle state){super.onCreate(state);registerPlugin(NativePrintPlugin.class);}
}
