package ru.kalinin.flight.dto.response;

/*{
        "content": [
        {
        "flightNumber": "17A",
        "departureCity": "MOSKOW"
        }
        ],
        "page": 1,
        "size": 10,
        "totalPages": 5,
        "totalElements": 42
        }*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalPages;
    private long totalElements;
}
