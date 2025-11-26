### How to run the app on an emulator while also running MAS locally.

1. To get the list of available Android Virtual Devices, run this command:
    ```
    ls -la ~/.android/avd/
    ```
    It will display the list of available devices

2. To launch the emulator, enter the following command (substitute "Pixel_XL_API_31" with the name of the emulator you're using, if different):
    ```
    export PHONE_NAME="Pixel_XL_API_31"; ${HOME}/Android/Sdk/emulator/emulator -avd ${PHONE_NAME} -writable-system
    ```

3. To obtain the list of connected adb devices, run the command: `adb devices`

4. Identify the name of your device from the list, which should resemble something like `amulator-5554`

5. In the terminal, execute the following commands:
    ```
    adb -s emulator-5554 root 
    adb -s emulator-5554 remount
    ```

    If prompted to reboot the emulator, run the command `adb -s emulator-5554 reboot`, and then repeat the above two commands.

6. Create a file called `hosts` with the following content:
    ```
    127.0.0.1       localhost
    ::1             ip6-localhost
    10.0.2.2 demo.gcp.concurrent.systems
    ```

7. Finally, push the `hosts` file to your emulator by running the command: 
`adb -s emulator-5554 push hosts /etc/hosts`


### Android Studio - IMPORTANT : Switching Emulator API Versions

In order to prevent environment and cache issues that can present in the form of BUGS in the code ... you must perform the following steps when switching Android Emulator API Versions in Android Studio... for example, you have been testing Android 8.1 (API 27) and desire to test on Android 12 (API 31) ... you must then follow these steps

1. Uninstall the app if it is present on the emulator you are switching to (say you move to API 31... uninstall the app on THAT emulator if it is present)
2. run `Build -> Clean Project` in the top menu of Android Studio
3. run `File -> Invalidate Caches` in the top menu of Android Studio ---- be sure to select the checkboxes: `Clear file systems cache.....` and `Clear VSC Log caches.....`

> NOTE:
> This is relevent when switching forward or backward in API versions


### Sandbox Mode

When working on the App, it is often required that we test layout changes and complete changes prior to having a functional MAS server. Therefore the SandboxRepository was born (previously called DummyRepository).

In order to enable Sandbox mode, change the value of `isSandboxEnabled` (near the bottom of this file). Along with this value, is a helper function that will automatically log you into the app ONLY if you are in sandbox mode, that function is `sandboxAutoLoginEnabled()`

> NOTE:
> If Sandbox mode is enabled, then ***None of the data in the app will be real***. This is of course intentional. It will also **not** make any outgoing network connections.


```
app/src/main/java/systems/concurrent/crediversemobile/repositories/SandboxRepository.kt


...
    private const val ENABLE_AUTO_LOGIN = true
    const val isSandboxEnabled = true

    // ...
    fun sandboxAutoLoginEnabled(): Boolean {
        return isSandboxEnabled && ENABLE_AUTO_LOGIN
    }
...
```


IMPORTANT: Because Sandbox allows you to test ALL functions of the app, any `Service` that is used, should incorporate the Sandbox mode, please see `MasService.kt` and `BundleService.kt` as examples, below a rough example of some functions of Sandbox mode that can make app testing easier


#### isTeamLead Example (with explanation comments)

```
    /**
     *
     * Functions from Services should always be ASYNC - the callback gets handled when the service responds
     *
     */

    fun isTeamLead(callback: (Result<Boolean>) -> Unit) {
        /**
         *
         * Here we check if we are in Sandbox mode, and return the relevant response in the callback
         *
         * Take note of the `delayedCallback` - this allows us to simulate a response taking longer...
         *
         */
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback(3000) {     // 500 milliseconds is the default if not specified
                callback(Result.success(true))

                /**
                 *
                 * Special Sandbox Handling of errors
                 *
                 *   If you want to simulate an error when using the Sandbox ...  like ErrorMessages.GET_MEMBERSHIP_FAILED
                 *   This is how you can respond in the callback with the Sandbox Error helper function
                 *
                 *
                 *   callback(
                 *      SandboxRepository.getFailureResultFromError(ErrorMessages.GET_MEMBERSHIP_FAILED)
                 *   )
                 *
                 *
                 */
            }

            /**
             * IMPORTANT ... you MUST return from Sandbox mode, otherwise it will proceed to make the request to the REAL MAS service below
             */
            return
        }

        /**
         * Here you write the REAL call to an external service like MAS ... not relevant for Sandbox mode
         */
        masService.isTeamLead { result ->
            result
                .onFailure {
                    /**
                     * AUXILLIARY INFO ... 
                     * 
                     *    in all known cases, we want to handle the error so we can translate it into an APP specific error
                     *    hence `handleSpecialResultOrReason`
                     *
                     */
                    callback(handleSpecialResultOrReason(it, /* default reason is INTERNAL SERVER ERROR if unspecified */))
                }
                .onSuccess {
                    callback(result)
                }
        }
    }
    ```

