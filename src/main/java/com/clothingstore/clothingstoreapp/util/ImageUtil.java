package com.clothingstore.clothingstoreapp.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageUtil {

    private static final int CONTENT_ALPHA_LIMIT = 120;
    private static final double CONTENT_FILL_RATIO = 0.92;
    private static final Map<String, Image> NORMALIZED_IMAGE_CACHE = new ConcurrentHashMap<>();

    private ImageUtil() {}

    public static Image loadProductImage(String path, double requestedWidth, double requestedHeight) {
        String cacheKey = cacheKey(path, requestedWidth, requestedHeight);
        Image cached = NORMALIZED_IMAGE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Image image = tryLoad(path);
        if (image != null && !image.isError()) {
            Image normalized = normalizeImage(image, requestedWidth, requestedHeight);
            NORMALIZED_IMAGE_CACHE.put(cacheKey, normalized);
            return normalized;
        }
        return createPlaceholder((int) Math.max(180, requestedWidth > 0 ? requestedWidth : 180),
                (int) Math.max(180, requestedHeight > 0 ? requestedHeight : 180));
    }

    private static String cacheKey(String path, double requestedWidth, double requestedHeight) {
        return String.valueOf(path).trim() + "|" + Math.round(requestedWidth) + "x" + Math.round(requestedHeight);
    }

    private static Image tryLoad(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        String normalized = path.trim();
        List<String> candidates = imagePathCandidates(normalized);

        // 1) classpath resources, e.g. /images/nike_black_tshirt.jpg
        for (String candidate : candidates) {
            String resourcePath = candidate.startsWith("/") ? candidate : "/" + candidate;
            java.net.URL resource = ImageUtil.class.getResource(resourcePath);
            if (resource != null) {
                Image img = new Image(resource.toExternalForm(), 0, 0, true, true, false);
                if (!img.isError()) return img;
            }
        }

        // 2) filesystem support for older DB paths
        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString(), 0, 0, true, true, false);
                if (!img.isError()) return img;
            }
        }

        for (String candidate : candidates) {
            File dotted = new File("." + candidate);
            if (dotted.exists()) {
                Image img = new Image(dotted.toURI().toString(), 0, 0, true, true, false);
                if (!img.isError()) return img;
            }
        }

        return null;
    }

    private static List<String> imagePathCandidates(String path) {
        List<String> candidates = new ArrayList<>();
        String pngPath = pngAlternativePath(path);
        if (pngPath != null) {
            candidates.add(pngPath);
        }
        candidates.add(path);
        return candidates.stream().distinct().toList();
    }

    private static String pngAlternativePath(String path) {
        int separatorIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex <= separatorIndex) {
            return null;
        }

        String extension = path.substring(dotIndex + 1).toLowerCase();
        if (!extension.equals("jpg") && !extension.equals("jpeg")) {
            return null;
        }

        return path.substring(0, dotIndex) + ".png";
    }

    private static Image normalizeImage(Image source, double requestedWidth, double requestedHeight) {
        int targetWidth = (int) Math.round(requestedWidth);
        int targetHeight = (int) Math.round(requestedHeight);
        if (targetWidth <= 0 || targetHeight <= 0 || source.getPixelReader() == null) {
            return source;
        }

        CropBounds crop = findContentBounds(source);
        int contentWidth = Math.max(1, (int) Math.round(targetWidth * CONTENT_FILL_RATIO));
        int contentHeight = Math.max(1, (int) Math.round(targetHeight * CONTENT_FILL_RATIO));
        double scale = Math.min(contentWidth / (double) crop.width(), contentHeight / (double) crop.height());
        int drawWidth = Math.max(1, (int) Math.round(crop.width() * scale));
        int drawHeight = Math.max(1, (int) Math.round(crop.height() * scale));
        int offsetX = (targetWidth - drawWidth) / 2;
        int offsetY = (targetHeight - drawHeight) / 2;

        WritableImage normalized = new WritableImage(targetWidth, targetHeight);
        PixelWriter writer = normalized.getPixelWriter();
        PixelReader reader = source.getPixelReader();

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                if (x < offsetX || x >= offsetX + drawWidth || y < offsetY || y >= offsetY + drawHeight) {
                    writer.setColor(x, y, Color.TRANSPARENT);
                    continue;
                }

                double sourceX = crop.x() + ((x - offsetX + 0.5) * crop.width() / drawWidth) - 0.5;
                double sourceY = crop.y() + ((y - offsetY + 0.5) * crop.height() / drawHeight) - 0.5;
                writer.setColor(x, y, sampleBilinear(reader, source, sourceX, sourceY));
            }
        }

        return normalized;
    }

    private static CropBounds findContentBounds(Image image) {
        PixelReader reader = image.getPixelReader();
        int width = Math.max(1, (int) Math.round(image.getWidth()));
        int height = Math.max(1, (int) Math.round(image.getHeight()));
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        int alphaPixelCount = 0;
        int foregroundMinX = width;
        int foregroundMinY = height;
        int foregroundMaxX = -1;
        int foregroundMaxY = -1;
        int foregroundPixelCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                int alpha = (argb >>> 24) & 0xff;
                if (alpha > CONTENT_ALPHA_LIMIT) {
                    alphaPixelCount++;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);

                    if (isForegroundColor(argb)) {
                        foregroundPixelCount++;
                        foregroundMinX = Math.min(foregroundMinX, x);
                        foregroundMinY = Math.min(foregroundMinY, y);
                        foregroundMaxX = Math.max(foregroundMaxX, x);
                        foregroundMaxY = Math.max(foregroundMaxY, y);
                    }
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return new CropBounds(0, 0, width, height);
        }

        CropBounds alphaBounds = new CropBounds(minX, minY, maxX - minX + 1, maxY - minY + 1);
        CropBounds foregroundBounds = foregroundMaxX < foregroundMinX || foregroundMaxY < foregroundMinY
                ? null
                : new CropBounds(foregroundMinX, foregroundMinY,
                foregroundMaxX - foregroundMinX + 1,
                foregroundMaxY - foregroundMinY + 1);

        if (shouldUseForegroundBounds(alphaBounds, foregroundBounds, alphaPixelCount, foregroundPixelCount)) {
            return foregroundBounds;
        }

        return alphaBounds;
    }

    private static boolean isForegroundColor(int argb) {
        int red = (argb >>> 16) & 0xff;
        int green = (argb >>> 8) & 0xff;
        int blue = argb & 0xff;
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));

        return max < 180 || max - min > 45;
    }

    private static boolean shouldUseForegroundBounds(
            CropBounds alphaBounds,
            CropBounds foregroundBounds,
            int alphaPixelCount,
            int foregroundPixelCount
    ) {
        if (foregroundBounds == null || alphaPixelCount <= 0) {
            return false;
        }

        double foregroundRatio = foregroundPixelCount / (double) alphaPixelCount;
        double alphaAspect = alphaBounds.width() / (double) alphaBounds.height();
        double foregroundAspect = foregroundBounds.width() / (double) foregroundBounds.height();

        return foregroundRatio > 0.12
                && foregroundBounds.width() > alphaBounds.width() * 0.35
                && foregroundBounds.height() > alphaBounds.height() * 0.35
                && foregroundAspect > alphaAspect * 1.10;
    }

    private static Color sampleBilinear(PixelReader reader, Image source, double sourceX, double sourceY) {
        int width = Math.max(1, (int) Math.round(source.getWidth()));
        int height = Math.max(1, (int) Math.round(source.getHeight()));
        double clampedX = clamp(sourceX, 0, width - 1);
        double clampedY = clamp(sourceY, 0, height - 1);

        int x0 = (int) Math.floor(clampedX);
        int y0 = (int) Math.floor(clampedY);
        int x1 = Math.min(width - 1, x0 + 1);
        int y1 = Math.min(height - 1, y0 + 1);
        double tx = clampedX - x0;
        double ty = clampedY - y0;

        Color top = interpolate(reader.getColor(x0, y0), reader.getColor(x1, y0), tx);
        Color bottom = interpolate(reader.getColor(x0, y1), reader.getColor(x1, y1), tx);
        return interpolate(top, bottom, ty);
    }

    private static Color interpolate(Color first, Color second, double ratio) {
        double inverse = 1.0 - ratio;
        return new Color(
                first.getRed() * inverse + second.getRed() * ratio,
                first.getGreen() * inverse + second.getGreen() * ratio,
                first.getBlue() * inverse + second.getBlue() * ratio,
                first.getOpacity() * inverse + second.getOpacity() * ratio
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Image createPlaceholder(int width, int height) {
        WritableImage img = new WritableImage(width, height);
        var writer = img.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double ratio = (double) y / Math.max(1, height - 1);
                Color color = ratio < 0.5 ? Color.web("#64DFDF") : Color.web("#56CFE1");

                // simple framed center block
                if (x > width * 0.12 && x < width * 0.88 && y > height * 0.12 && y < height * 0.88) {
                    color = Color.web("#72EFDD");
                }

                writer.setColor(x, y, color);
            }
        }

        // dark border
        for (int x = 0; x < width; x++) {
            writer.setColor(x, 0, Color.web("#7400B8"));
            writer.setColor(x, height - 1, Color.web("#7400B8"));
        }
        for (int y = 0; y < height; y++) {
            writer.setColor(0, y, Color.web("#7400B8"));
            writer.setColor(width - 1, y, Color.web("#7400B8"));
        }

        return img;
    }

    private record CropBounds(int x, int y, int width, int height) {}
}



