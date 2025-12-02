package info.benjaminhill.geochat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The application entry point for the Geochat app.
 *
 * **Purpose:**
 * This class is required by Hilt to trigger the dependency injection code generation.
 * By annotating it with `@HiltAndroidApp`, we tell Hilt to create the base component
 * that lives as long as the application does.
 *
 * **Architecture:**
 * - **Layer:** Application Framework.
 * - **Relations:**
 *   - Parent of all other components in the app lifecycle.
 *
 * **Why keep it?**
 * Without this class, Hilt dependency injection will not work, and the app will crash on startup.
 */
@HiltAndroidApp
class GeochatApplication : Application()
