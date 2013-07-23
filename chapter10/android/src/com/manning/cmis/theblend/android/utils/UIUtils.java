/*******************************************************************************
 * Copyright 2012 Manning Publications Co.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.manning.cmis.theblend.android.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;

/**
 * List of UI utility Methods.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class UIUtils {

    /**
     * Block screen rotation of the specified activity.
     * 
     * @param activity
     *            :
     */
    public static void blockScreenOrientation(Activity activity) {
        activity.setRequestedOrientation(activity.getResources().getConfiguration().orientation);
    }

    /**
     * Allow screen rotation of the specified activity.
     * 
     * @param activity
     */
    public static void unBlockScreenOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

}
