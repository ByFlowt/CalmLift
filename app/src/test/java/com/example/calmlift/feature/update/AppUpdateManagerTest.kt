package com.example.calmlift.feature.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateManagerTest {

    @Test
    fun `resolveAuth decodes non-empty token`() {
        val token = GitHubAuth.resolveAuth()
        assertTrue("Decoded token should not be empty", token.isNotBlank())
    }

    @Test
    fun `isNewerVersion handles semver comparisons accurately`() {
        // Simple patch bump
        assertTrue(AppUpdateManager.isNewerVersion("1.0.1", "1.0.0"))
        assertTrue(AppUpdateManager.isNewerVersion("1.1.28", "1.1.25"))

        // Minor and major bumps
        assertTrue(AppUpdateManager.isNewerVersion("1.2.0", "1.1.99"))
        assertTrue(AppUpdateManager.isNewerVersion("2.0.0", "1.9.9"))

        // With 'v' prefix
        assertTrue(AppUpdateManager.isNewerVersion("v1.0.5", "1.0.4"))
        assertTrue(AppUpdateManager.isNewerVersion("V2.1.0", "v2.0.9"))

        // Same or older versions
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(AppUpdateManager.isNewerVersion("1.0.0", "1.0.1"))
        assertFalse(AppUpdateManager.isNewerVersion("0.9.9", "1.0.0"))

        // Suffixes and tags
        assertTrue(AppUpdateManager.isNewerVersion("1.1.0-release", "1.0.9"))
    }
}
