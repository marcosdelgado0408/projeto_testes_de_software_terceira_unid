package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel()) {
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado()) {
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso()) {
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	}

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {
		BigDecimal totalProdutos = BigDecimal.ZERO;
	    int pesoTotal = 0;

	    // Calcular o custo total dos produtos e o peso total
	    for (ItemCompra item : carrinho.getItens()) {
	        BigDecimal precoProduto = item.getProduto().getPreco();
	        Long quantidade = item.getQuantidade();
	        totalProdutos = totalProdutos.add(precoProduto.multiply(BigDecimal.valueOf(quantidade)));
	        pesoTotal += item.getProduto().getPeso() * quantidade;
	    }

	    // Calcular o custo do frete
	    BigDecimal custoFrete = calcularFrete(pesoTotal, carrinho.getCliente().getTipo());

	    // Aplicar descontos se necessário
	    if (totalProdutos.compareTo(BigDecimal.valueOf(1000)) > 0) {
	        totalProdutos = totalProdutos.multiply(BigDecimal.valueOf(0.8)); // 20% de desconto
	    } else if (totalProdutos.compareTo(BigDecimal.valueOf(500)) > 0) {
	        totalProdutos = totalProdutos.multiply(BigDecimal.valueOf(0.9)); // 10% de desconto
	    }

	    return totalProdutos.add(custoFrete);
	}
	
	private BigDecimal calcularFrete(int pesoTotal, TipoCliente tipoCliente) {
	    BigDecimal custoFrete = BigDecimal.ZERO;

	    if (pesoTotal > 50) {
	        custoFrete = BigDecimal.valueOf(7).multiply(BigDecimal.valueOf(pesoTotal));
	    } else if (pesoTotal >= 10) {
	        custoFrete = BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(pesoTotal));
	    } else if (pesoTotal >= 5) {
	        custoFrete = BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(pesoTotal));
	    }

	    // Aplicar isenção de frete
	    if (tipoCliente == TipoCliente.OURO) {
	        return BigDecimal.ZERO;
	    } else if (tipoCliente == TipoCliente.PRATA) {
	        return custoFrete.multiply(BigDecimal.valueOf(0.5));
	    }

	    return custoFrete;
	}
}
