package kyo.yaz.condominium.manager.core.service;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

public class CronTest {

  @Test
  void testCron() {
    var expression = CronExpression.parse("*    */5    13,14,15,16,17,23    *    *    MON-FRI");
    var result = expression.next(LocalDateTime.now());
    System.out.println(result);
    System.out.println(expression);
  }
}
