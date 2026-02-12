//package com.commercepal.apiservice.promotions.promo;
//
//import com.commercepal.apiservice.promotions.promo.dto.PromoCodeRequest;
//import com.commercepal.apiservice.utils.response.ResponseWrapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.validation.Valid;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/promo-codes")
//@RequiredArgsConstructor
//public class PromoCodeController {
//
//    private final PromoCodeService promoCodeService;
//
//    @PostMapping
//    public ResponseEntity<ResponseWrapper<PromoCode>> create(@RequestBody @Valid PromoCodeRequest request) {
//        return ResponseWrapper.success(promoCodeService.createPromoCode(request));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ResponseWrapper<PromoCode>> update(@PathVariable Long id, @RequestBody PromoCodeRequest request) {
//        return ResponseWrapper.success(promoCodeService.updatePromoCode(id, request));
//    }
//
//    @GetMapping("/active")
//    public ResponseEntity<ResponseWrapper<List<PromoCode>>> getActivePromoCodes() {
//        return ResponseWrapper.success(promoCodeService.getAllActivePromoCodes());
//    }
//
//    @GetMapping
//    public ResponseEntity<ResponseWrapper<List<PromoCode>>> getAll() {
//        return ResponseWrapper.success(promoCodeService.getAll());
//    }
//
//    @GetMapping("/by-code")
//    public ResponseEntity<ResponseWrapper<PromoCode>> getByCode(@RequestParam String code) {
//        return ResponseWrapper.success(promoCodeService.getByCode(code));
//    }
//}
