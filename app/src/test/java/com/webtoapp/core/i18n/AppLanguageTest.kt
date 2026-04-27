package com.webtoapp.core.i18n

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `fromCode resolves supported languages and defaults to english`() {
        assertThat(AppLanguage.fromCode("en")).isEqualTo(AppLanguage.ENGLISH)
        assertThat(AppLanguage.fromCode("hi")).isEqualTo(AppLanguage.HINDI)
        assertThat(AppLanguage.fromCode("unknown")).isEqualTo(AppLanguage.ENGLISH)
    }

    @Test
    fun `language metadata includes locale information`() {
        assertThat(AppLanguage.ENGLISH.locale.language).isEqualTo("en")
        assertThat(AppLanguage.HINDI.locale.language).isEqualTo("hi")
    }
}
