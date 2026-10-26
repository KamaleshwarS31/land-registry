package com.landregistry.evidence;

public class StorageUploadResult {
    private String fileName;
    private String storagePath;
    private String storageProvider; // SUPABASE or LOCAL
    private String sha256Hash;
    private long fileSize;
    private String publicOrSignedUrl;

    public StorageUploadResult() {}

    public StorageUploadResult(String fileName, String storagePath, String storageProvider, String sha256Hash, long fileSize, String publicOrSignedUrl) {
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.storageProvider = storageProvider;
        this.sha256Hash = sha256Hash;
        this.fileSize = fileSize;
        this.publicOrSignedUrl = publicOrSignedUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }
    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getPublicOrSignedUrl() { return publicOrSignedUrl; }
    public void setPublicOrSignedUrl(String publicOrSignedUrl) { this.publicOrSignedUrl = publicOrSignedUrl; }

    public static class Builder {
        private String fileName;
        private String storagePath;
        private String storageProvider;
        private String sha256Hash;
        private long fileSize;
        private String publicOrSignedUrl;

        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder storagePath(String storagePath) { this.storagePath = storagePath; return this; }
        public Builder storageProvider(String storageProvider) { this.storageProvider = storageProvider; return this; }
        public Builder sha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public Builder publicOrSignedUrl(String publicOrSignedUrl) { this.publicOrSignedUrl = publicOrSignedUrl; return this; }

        public StorageUploadResult build() {
            return new StorageUploadResult(fileName, storagePath, storageProvider, sha256Hash, fileSize, publicOrSignedUrl);
        }
    }
}
