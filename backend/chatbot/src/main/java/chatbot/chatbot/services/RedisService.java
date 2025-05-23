package chatbot.chatbot.services;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveHistory(String userId, String data) {
        redisTemplate.opsForValue().append(userId, data);
        redisTemplate.expire(userId, Duration.ofMinutes(30));
    }

    public String getHistory(String userId) {
        return redisTemplate.opsForValue().get(userId);
    }

    public void deleteHistory(String userId) {
        redisTemplate.delete(userId);
    }
}
