package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaDTO<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PaginaDTO<T> from(Page<T> page) {
        return new PaginaDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}