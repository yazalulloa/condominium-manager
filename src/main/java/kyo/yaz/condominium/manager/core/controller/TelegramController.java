package kyo.yaz.condominium.manager.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TelegramController.PATH)
public class TelegramController {

  public static final String PATH = "/0570b232-ab43-4242-8a9e-d5f035ef7580";

  @PostMapping(path = "/webhook")
  public ResponseEntity<?> webhook(@RequestBody String json) {
    return ResponseEntity.ok().build();
  }
}
