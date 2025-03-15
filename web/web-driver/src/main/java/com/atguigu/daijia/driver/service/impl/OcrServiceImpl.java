package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.client.OcrFeignClient;
import com.atguigu.daijia.driver.service.OcrService;
import com.atguigu.daijia.model.vo.driver.DriverLicenseOcrVo;
import com.atguigu.daijia.model.vo.driver.IdCardOcrVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class OcrServiceImpl implements OcrService {

   private final OcrFeignClient ocrFeignClient;

    //身份证识别
    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {

        Result<IdCardOcrVo> ocrVoResult = ocrFeignClient.idCardOcr(file);
        return ocrVoResult.getData();   //返回vo对象
    }

    //驾驶证识别
    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        Result<DriverLicenseOcrVo> driverLicenseOcrVoResult = ocrFeignClient.driverLicenseOcr(file);
        return driverLicenseOcrVoResult.getData();
    }
}
