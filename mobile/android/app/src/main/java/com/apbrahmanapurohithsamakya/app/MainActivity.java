package com.apbrahmanapurohithsamakya.app;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
 @Override public void onCreate(Bundle state){registerPlugin(NativePrintPlugin.class);super.onCreate(state);}
}
