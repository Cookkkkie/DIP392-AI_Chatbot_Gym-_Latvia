package chatbot.chatbot.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    private final String userId = "52";
    private final String message = "hello";
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void saveHistory_appendsDataAndSetsExpiry() {
        redisService.saveHistory(userId, message);

        verify(valueOps).append(userId, message);
        verify(redisTemplate).expire(userId, Duration.ofMinutes(30));
    }

    @Test
    void saveHistory_expiresAfterThirtyMinutes() {
        redisService.saveHistory(userId, message);

        ArgumentCaptor<Duration> captor = ArgumentCaptor.forClass(Duration.class);
        verify(redisTemplate).expire(eq(userId), captor.capture());

        assertEquals(
                Duration.ofMinutes(30),
                captor.getValue(),
                "History key should expire exactly 30 minutes after save"
        );
    }

    @Test
    void getHistory_returnsValueFromRedis() {
        
        when(valueOps.get(userId)).thenReturn("previous");

        String history = redisService.getHistory(userId);

        assertEquals("previous", history);
        verify(valueOps).get(userId);
    }

    @Test
    void deleteHistory_deletesKey() {
        redisService.deleteHistory(userId);
        verify(redisTemplate).delete(userId);
    }
}
