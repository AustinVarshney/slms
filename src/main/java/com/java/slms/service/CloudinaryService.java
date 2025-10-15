package com.java.slms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @param folder Folder name in Cloudinary
     * @return Map containing upload result with url and public_id
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "transformation", ObjectUtils.asMap(
                    "quality", "auto",
                    "fetch_format", "auto"
                )
            )
        );

        return uploadResult;
    }

    /**
     * Delete image from Cloudinary
     * @param publicId Public ID of the image to delete
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Extract public ID from Cloudinary URL
     * @param url Cloudinary URL
     * @return Public ID
     */
    public String extractPublicId(String url) {
        // Extract public ID from URL
        // Example: https://res.cloudinary.com/cloud-name/image/upload/v1234567890/folder/image.jpg
        // Public ID: folder/image
        if (url == null || !url.contains("cloudinary.com")) {
            return null;
        }

        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;

            String afterUpload = parts[1];
            // Remove version number (v1234567890/)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove file extension
            String publicId = withoutVersion.substring(0, withoutVersion.lastIndexOf('.'));
            
            return publicId;
        } catch (Exception e) {
            return null;
        }
    }
}
