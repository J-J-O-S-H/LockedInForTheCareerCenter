package com.lockedin.careercenter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.lockedin.careercenter.repository.EventRepository;
import com.lockedin.careercenter.repository.UserRepository;

@SpringBootTest
class LockedInCareerCenterApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @Test
    void contextLoads() {
    }
}
