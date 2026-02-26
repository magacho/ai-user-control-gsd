package com.bemobi.aiusercontrol.integration.cursor;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursorApiClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    private CursorApiClient cursorApiClient;

    @BeforeEach
    void setUp() {
        cursorApiClient = new CursorApiClient(webClientBuilder);
    }

    @Test
    void testFetchUsers_success_returnsActiveMembers() {
        // Given: Response with 2 active members and 1 removed member
        Map<String, Object> member1 = new HashMap<>();
        member1.put("email", "alice@bemobi.com");
        member1.put("isRemoved", false);

        Map<String, Object> member2 = new HashMap<>();
        member2.put("email", "bob@bemobi.com");
        member2.put("isRemoved", false);

        Map<String, Object> removedMember = new HashMap<>();
        removedMember.put("email", "removed@bemobi.com");
        removedMember.put("isRemoved", true);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("teamMembers", List.of(member1, member2, removedMember));

        mockWebClientResponse(responseBody);

        // When
        List<ToolAccountInfo> result = cursorApiClient.fetchUsers("test-api-key", "https://api.cursor.com", "org-123");

        // Then: Only 2 active members returned (removed filtered out)
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("alice@bemobi.com", result.get(0).getIdentifier());
        assertEquals("alice@bemobi.com", result.get(0).getEmail());
        assertEquals("bob@bemobi.com", result.get(1).getIdentifier());
        assertEquals("bob@bemobi.com", result.get(1).getEmail());
    }

    @Test
    void testFetchUsers_nullApiKey_returnsEmptyList() {
        // When: null apiKey
        List<ToolAccountInfo> result = cursorApiClient.fetchUsers(null, "https://api.cursor.com", "org-123");

        // Then: returns empty list without HTTP call
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(webClientBuilder, never()).baseUrl(anyString());
    }

    @Test
    void testFetchUsers_blankApiKey_returnsEmptyList() {
        // When: blank apiKey
        List<ToolAccountInfo> result = cursorApiClient.fetchUsers("  ", "https://api.cursor.com", "org-123");

        // Then: returns empty list without HTTP call
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(webClientBuilder, never()).baseUrl(anyString());
    }

    @Test
    void testFetchUsers_apiError_returnsEmptyList() {
        // Given: WebClient throws exception
        when(webClientBuilder.baseUrl(anyString())).thenThrow(new RuntimeException("Connection timeout"));

        // When
        List<ToolAccountInfo> result = cursorApiClient.fetchUsers("test-api-key", "https://api.cursor.com", "org-123");

        // Then: returns empty list (graceful failure)
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchUsers_emptyTeamMembers_returnsEmptyList() {
        // Given: Response with empty teamMembers
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("teamMembers", Collections.emptyList());

        mockWebClientResponse(responseBody);

        // When
        List<ToolAccountInfo> result = cursorApiClient.fetchUsers("test-api-key", "https://api.cursor.com", "org-123");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchUsers_defaultBaseUrl_usesApiCursorCom() {
        // Given: Response with one member
        Map<String, Object> member = new HashMap<>();
        member.put("email", "alice@bemobi.com");
        member.put("isRemoved", false);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("teamMembers", List.of(member));

        mockWebClientResponse(responseBody);

        // When: null apiBaseUrl
        List<ToolAccountInfo> result = cursorApiClient.fetchUsers("test-api-key", null, "org-123");

        // Then: WebClient built with default URL
        verify(webClientBuilder).baseUrl("https://api.cursor.com");
        assertEquals(1, result.size());
    }

    @Test
    void testFetchUsers_basicAuthEncoding_correctFormat() {
        // Given
        String apiKey = "my-cursor-key";
        String expectedEncoded = Base64.getEncoder().encodeToString((apiKey + ":").getBytes(StandardCharsets.UTF_8));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("teamMembers", Collections.emptyList());

        mockWebClientResponse(responseBody);

        // When
        cursorApiClient.fetchUsers(apiKey, "https://api.cursor.com", "org-123");

        // Then: Authorization header uses correct Basic Auth format
        verify(webClientBuilder).defaultHeader("Authorization", "Basic " + expectedEncoded);
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientResponse(Map<String, Object> responseBody) {
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(responseBody));
    }
}
