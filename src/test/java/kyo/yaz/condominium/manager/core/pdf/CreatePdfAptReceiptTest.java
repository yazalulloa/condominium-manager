package kyo.yaz.condominium.manager.core.pdf;

import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class CreatePdfAptReceiptTest {

  @Autowired
  private BuildingService buildingService;
  @Autowired
  private ReceiptService receiptService;
  @Autowired
  private ApartmentService apartmentService;

  @Test
  void test() {

  }
}