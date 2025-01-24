package ecommerce;

import ecommerce.entity.Cliente;
import ecommerce.repository.ClienteRepository;
import ecommerce.service.ClienteService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;  // Mock do repositório

    @InjectMocks
    private ClienteService clienteService;  // Injeção do mock no serviço

    @Test
    void testBuscarPorId_ClienteEncontrado() {
        // Dado que temos um cliente no repositório
        Long clienteId = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        // Quando o repositório retorna o cliente
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Então o cliente deve ser retornado
        Cliente resultado = clienteService.buscarPorId(clienteId);
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
    }

    @Test
    void testBuscarPorId_ClienteNaoEncontrado() {
        // Dado que temos um cliente com ID inexistente
        Long clienteId = 1L;

        // Quando o repositório não encontra o cliente
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Então uma IllegalArgumentException deve ser lançada
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.buscarPorId(clienteId);
        });
        assertEquals("Cliente não encontrado", exception.getMessage());
    }
}
