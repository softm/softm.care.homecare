package net.softm.data.kata;

//import org.mockito.Mockito
import android.app.Activity
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.android.controller.ComponentController
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(application = WApplication::class,sdk = [28], manifest = Config.NONE)
class MainActivityTest {


    lateinit var fakeUpdateManager: FakeAppUpdateManager
    lateinit var mainActivityController: ActivityController<MainActivity>
    lateinit var mainActivity: MainActivity

    @Before
    fun setup() {
        fakeUpdateManager = FakeAppUpdateManager(WApplication.mInstance)
        mainActivityController = Robolectric.buildActivity(MainActivity::class.java)
        mainActivity = Mockito.spy(mainActivityController.get())
//        fakeUpdateManager = ApplicationProvider.getApplicationContext<WApplication>().fakeAppUpdateManager
        replaceComponentInActivityController(mainActivityController, mainActivity)
    }

    @Test
    fun `immediate update flow`() {
//        fakeUpdateManager.setUpdateAvailable(211)
//        assert(fakeUpdateManager.isImmediateFlowVisible)
        fakeUpdateManager.setUpdateAvailable(211,AppUpdateType.IMMEDIATE)
        // Validate that immediate update is prompted to the user.
//        assertTrue(fakeUpdateManager.isImmediateFlowVisible)
        fakeUpdateManager.isImmediateFlowVisible()
//        assert(fakeUpdateManager.isConfirmationDialogVisible());

        // Simulate user's and download behavior.
        fakeUpdateManager.userAcceptsUpdate()

        fakeUpdateManager.downloadStarts()

        fakeUpdateManager.downloadCompletes()

        // Validate that update is completed and app is restarted.
        assert( fakeUpdateManager.isInstallSplashScreenVisible());

    }

    @Test
    fun `update not available`() {
        mainActivityController.create()
//        Mockito.verify(mainActivity).showNoUpdateInfo()
    }


    @Test
    fun `flexible update flow`() {
        fakeUpdateManager.setUpdateAvailable(2)
        mainActivityController.create()
        assert(fakeUpdateManager.isConfirmationDialogVisible)

        fakeUpdateManager.userAcceptsUpdate()
        fakeUpdateManager.downloadStarts()
        fakeUpdateManager.downloadCompletes()

        Mockito.verify(fakeUpdateManager).completeUpdate()
    }


    private fun replaceComponentInActivityController(activityController: ActivityController<*>, activity: Activity) {
        val componentField = ComponentController::class.java.getDeclaredField("component")
        componentField.isAccessible = true
        componentField.set(activityController, activity)
    }


}
//class TestKt {
//
//
//    @Test
//    fun testFlexibleUpdate_Completes() {
//        FakeAppUpdateManager fakeAppUpdateManager = FakeAppUpdateManager(context)
//        // Setup flexible update.
//        fakeAppUpdateManager.partiallyAllowedUpdateType = AppUpdateType.FLEXIBLE
//        fakeAppUpdateManager.setUpdateAvailable(2)
//
//        ActivityScenario.launch(MainActivity::class.java)
//
//        // Validate that flexible update is prompted to the user.
//        assertTrue(fakeAppUpdateManager.isConfirmationDialogVisible)
//
//        // Simulate user's and download behavior.
//        fakeAppUpdateManager.userAcceptsUpdate()
//
//        fakeAppUpdateManager.downloadStarts()
//
//        fakeAppUpdateManager.downloadCompletes()
//
//        // Perform a click on the Snackbar to complete the update process.
//        onView(
//            allOf(
//                isDescendantOfA(instanceOf(Snackbar.SnackbarLayout::class.java)),
//                instanceOf(AppCompatButton::class.java)
//            )
//        ).perform(ViewActions.click())
//
//        // Validate that update is completed and app is restarted.
//        assertTrue(fakeAppUpdateManager.isInstallSplashScreenVisible)
//
//        fakeAppUpdateManager.installCompletes()
//    }
//
//}