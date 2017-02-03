/**
 * 
 */
package com.hollowsxd.autop;

//Imports
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.*;

//Main Function by drudge
/**
 * Created by drudge on 12/8/13
 * Forked from https://gist.github.com/drudge/7884571
 * I just import the code and make it into modules as old modules has disappeared from the web
 * -HollowsxD
 */
public class Main implements IXposedHookLoadPackage {
    public static final String PACKAGE_NAME = "com.android.keyguard";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!PACKAGE_NAME.equals(lpparam.packageName))
            return;

        final Class<?> clsKeyguardAbsKeyInputView = findClass(PACKAGE_NAME + ".KeyguardAbsKeyInputView", lpparam.classLoader);

        XposedBridge.log("Attempting to hook keyguard input view.");

        findAndHookMethod(clsKeyguardAbsKeyInputView, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                final TextView mPasswordEntry = (TextView) getObjectField(param.thisObject, "mPasswordEntry");
                XposedBridge.log("Successfully hooked into package.");
                
                if (mPasswordEntry != null) {
                    mPasswordEntry.addTextChangedListener(new TextWatcher()
                    {
                    	{XposedBridge.log("Adding listener to key entry");}
                    	
                        @Override
                        public void afterTextChanged(Editable s) {
                            final Object mCallback = getObjectField(param.thisObject, "mCallback");
                            final Object mLockPatternUtils = getObjectField(param.thisObject, "mLockPatternUtils");
                            String pass = mPasswordEntry.getText().toString();

                            if (mCallback != null && mLockPatternUtils != null && pass.length() > 3 &&
                                    (Boolean) callMethod(mLockPatternUtils, "checkPassword", pass)) {
                                callMethod(mCallback, "reportSuccessfulUnlockAttempt");
                                XposedBridge.log("Success auto-pin-ed");
                                callMethod(mCallback, "dismiss", true);
                                XposedBridge.log("Auto-pin error - wrong pin");
                            }
                        }

                        @Override
                        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

                        @Override
                        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
                    });
                }
            }
        });
    }
}
