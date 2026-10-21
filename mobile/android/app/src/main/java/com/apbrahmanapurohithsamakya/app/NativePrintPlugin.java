package com.apbrahmanapurohithsamakya.app;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
@CapacitorPlugin(name="NativePrint")
public class NativePrintPlugin extends Plugin {
 @PluginMethod public void print(PluginCall call){
  getActivity().runOnUiThread(()->{
   try{
    PrintManager manager=(PrintManager)getContext().getSystemService(Context.PRINT_SERVICE);
    if(manager==null){call.reject("Android printing is unavailable");return;}
    manager.print("AP Purohit Member ID",getBridge().getWebView().createPrintDocumentAdapter("AP Purohit Member ID"),new PrintAttributes.Builder().build());
    call.resolve();
   }catch(RuntimeException error){call.reject("Unable to open Android printing");}
  });
 }
}
