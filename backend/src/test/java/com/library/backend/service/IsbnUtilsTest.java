package com.library.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsbnUtilsTest {

    @Test
    void normalizesSpacesAndHyphens() {
        assertThat(IsbnUtils.normalize("978-0-306-40615-7")).isEqualTo("9780306406157");
    }

    @Test
    void acceptsValidIsbn10And13() {
        assertThat(IsbnUtils.isValid("0-306-40615-2")).isTrue();
        assertThat(IsbnUtils.isValid("978-0-306-40615-7")).isTrue();
    }

    @Test
    void acceptsIsbn10WithXCheckDigit() {
        assertThat(IsbnUtils.isValid("0-8044-2957-X")).isTrue();
    }

    @Test
    void rejectsInvalidLengthCharactersAndChecksum() {
        assertThat(IsbnUtils.isValid("123")) .isFalse();
        assertThat(IsbnUtils.isValid("9780306406158")).isFalse();
        assertThat(IsbnUtils.isValid("0-306-40615-X")).isFalse();
    }
}
