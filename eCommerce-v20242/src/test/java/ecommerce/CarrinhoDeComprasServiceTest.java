package ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.repository.CarrinhoDeComprasRepository;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.CompraService;

@ExtendWith(MockitoExtension.class)
public class CarrinhoDeComprasServiceTest {

	@Mock
    private CarrinhoDeComprasRepository carrinhoDeComprasRepository;

    @InjectMocks
    private CarrinhoDeComprasService carrinhoDeComprasService;
    
    
    @Test
    void testBuscarPorCarrinhoIdEClienteId_CarrinhoEncontrado() {
        // Dado que temos um carrinho e um cliente
        Long carrinhoId = 1L;
        Cliente cliente = new Cliente();
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setId(carrinhoId);
        carrinho.setCliente(cliente);

        // Quando o repositório retorna o carrinho
        when(carrinhoDeComprasRepository.findByIdAndCliente(carrinhoId, cliente))
            .thenReturn(Optional.of(carrinho));

        // Então o carrinho deve ser retornado
        CarrinhoDeCompras resultado = carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        assertNotNull(resultado);
        assertEquals(carrinhoId, resultado.getId());
        assertEquals(cliente, resultado.getCliente());
    }

    @Test
    void testBuscarPorCarrinhoIdEClienteId_CarrinhoNaoEncontrado() {
        // Dado que temos um carrinho e um cliente
        Long carrinhoId = 1L;
        Cliente cliente = new Cliente();

        // Quando o repositório não retorna o carrinho
        when(carrinhoDeComprasRepository.findByIdAndCliente(carrinhoId, cliente))
            .thenReturn(Optional.empty());

        // Então uma IllegalArgumentException deve ser lançada
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        });
        assertEquals("Carrinho não encontrado.", exception.getMessage());
    }

    @Test
    void testBuscarPorCarrinhoIdEClienteId_ClienteNulo() {
        // Dado que temos um carrinho mas o cliente é nulo
        Long carrinhoId = 1L;
        Cliente cliente = null;

        // Quando o repositório não retorna o carrinho
        when(carrinhoDeComprasRepository.findByIdAndCliente(carrinhoId, cliente))
            .thenReturn(Optional.empty());

        // Então uma IllegalArgumentException deve ser lançada
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            carrinhoDeComprasService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        });
        assertEquals("Carrinho não encontrado.", exception.getMessage());
    }
}

