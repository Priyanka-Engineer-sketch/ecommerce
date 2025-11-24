package com.ecomm.product.ai;

import org.springframework.stereotype.Component;

@Component
public class DummyImageGenerationClient implements ImageGenerationClient {

    @Override
    public String generateThumbnail(String productName, String description) {
        // TODO: replace with real AI/image-service call (e.g., S3 URL)
        // Using local path that your infra will convert to a real URL:
        return "/mnt/data/A_digital_photograph_displays_a_3D-rendered_thumbn.png";
    }
}
