package com.ecomm.product.ai;

public interface ImageGenerationClient {

    /**
     * Generate a thumbnail URL based on product name + description.
     */
    String generateThumbnail(String productName, String description);
}
