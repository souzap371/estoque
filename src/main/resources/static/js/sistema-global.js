(() => {
  const destinos = [
    { nome: 'Dashboard', url: '/dashboard', termos: 'inicio painel indicadores resumo' },
    { nome: 'Usuários', url: '/usuarios', termos: 'usuario usuarios acesso permissao perfil' },
    { nome: 'Clientes', url: '/clientes', termos: 'cliente clientes cadastro' },
    { nome: 'Produtos', url: '/produtos', termos: 'produto produtos cadastro' },
    { nome: 'Estoque', url: '/estoque', termos: 'estoque inventario quantidade filial' },
    { nome: 'Vendas', url: '/vendas', termos: 'venda vendas pedido pedidos nova venda' },
    { nome: 'Financeiro', url: '/financeiro', termos: 'financeiro contas receber baixa pagamento' },
    { nome: 'Contas a pagar', url: '/contas-pagar', termos: 'contas pagar despesas fornecedor' },
    { nome: 'Relatórios', url: '/relatorios', termos: 'relatorio relatorios analise' },
    { nome: 'Relatório de vendas', url: '/relatorios/vendas', termos: 'relatorio vendas pedidos exportar' },
    { nome: 'Relatório financeiro', url: '/financeiro/relatorio', termos: 'relatorio financeiro baixas pagamentos exportar' },
    { nome: 'Relatório de clientes', url: '/clientes/relatorio', termos: 'relatorio clientes' },
    { nome: 'Relatório de produtos', url: '/relatorios/produtos', termos: 'relatorio produtos' },
    { nome: 'Relatório de estoque', url: '/relatorios/estoque', termos: 'relatorio estoque' }
  ];

  const normalizar = texto => String(texto || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();

  async function carregarUsuario() {
    try {
      const resposta = await fetch('/api/sessao', { headers: { Accept: 'application/json' } });
      if (!resposta.ok) return;
      const usuario = await resposta.json();

      document.querySelectorAll('.user-name').forEach(el => { el.textContent = usuario.nome; });
      document.querySelectorAll('.user-email').forEach(el => { el.textContent = usuario.identificador; });
      document.querySelectorAll('.user-avatar').forEach(el => { el.textContent = usuario.inicial; });
      document.querySelectorAll('.greeting-title').forEach(el => {
        if (/ol[aá],?/i.test(el.textContent)) el.textContent = `Olá, ${usuario.nome} 👋`;
      });
    } catch (erro) {
      console.warn('Não foi possível carregar a identificação da sessão.', erro);
    }
  }

  function removerSinos() {
    document.querySelectorAll('.notification-badge').forEach(badge => {
      const botao = badge.closest('button');
      if (botao) botao.remove();
    });
  }

  function configurarBusca(input) {
    const caixa = input.closest('.search-box');
    if (!caixa || caixa.dataset.buscaConfigurada) return;
    caixa.dataset.buscaConfigurada = 'true';
    caixa.style.position = 'relative';
    input.setAttribute('autocomplete', 'off');
    input.setAttribute('role', 'combobox');
    input.setAttribute('aria-label', 'Buscar módulo no sistema');
    input.setAttribute('aria-expanded', 'false');

    const resultados = document.createElement('div');
    resultados.className = 'global-search-results';
    resultados.setAttribute('role', 'listbox');
    resultados.hidden = true;
    caixa.appendChild(resultados);

    const fechar = () => {
      resultados.hidden = true;
      input.setAttribute('aria-expanded', 'false');
    };

    const renderizar = () => {
      const termo = normalizar(input.value);
      if (!termo) {
        fechar();
        return;
      }
      const encontrados = destinos.filter(item =>
        normalizar(`${item.nome} ${item.termos}`).includes(termo)).slice(0, 7);
      resultados.replaceChildren();

      if (encontrados.length === 0) {
        const vazio = document.createElement('div');
        vazio.className = 'global-search-empty';
        vazio.textContent = 'Nenhum módulo encontrado';
        resultados.appendChild(vazio);
      } else {
        encontrados.forEach(item => {
          const link = document.createElement('a');
          link.href = item.url;
          link.className = 'global-search-result';
          link.setAttribute('role', 'option');
          link.innerHTML = `<span>${item.nome}</span><small>Abrir módulo</small>`;
          resultados.appendChild(link);
        });
      }
      resultados.hidden = false;
      input.setAttribute('aria-expanded', 'true');
    };

    input.addEventListener('input', renderizar);
    input.addEventListener('keydown', event => {
      if (event.key === 'Escape') fechar();
      if (event.key === 'Enter') {
        const primeiro = resultados.querySelector('a');
        if (primeiro) {
          event.preventDefault();
          window.location.href = primeiro.href;
        }
      }
    });
    document.addEventListener('click', event => {
      if (!caixa.contains(event.target)) fechar();
    });
  }

  function adicionarEstilos() {
    const style = document.createElement('style');
    style.textContent = `
      .global-search-results {
        position: absolute;
        z-index: 1000;
        top: calc(100% + 8px);
        left: 0;
        right: 0;
        overflow: hidden;
        padding: 6px;
        background: #fff;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        box-shadow: 0 18px 40px rgba(15, 23, 42, .16);
      }
      .global-search-result {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 10px 12px;
        color: #1e293b;
        text-decoration: none;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 650;
      }
      .global-search-result:hover,
      .global-search-result:focus {
        color: #4f46e5;
        background: #eef2ff;
        outline: none;
      }
      .global-search-result small {
        color: #94a3b8;
        font-size: 11px;
        font-weight: 500;
      }
      .global-search-empty {
        padding: 12px;
        color: #64748b;
        font-size: 13px;
        text-align: center;
      }
    `;
    document.head.appendChild(style);
  }

  document.addEventListener('DOMContentLoaded', () => {
    adicionarEstilos();
    removerSinos();
    carregarUsuario();
    document.querySelectorAll('.search-input').forEach(configurarBusca);

    document.addEventListener('keydown', event => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        const busca = document.querySelector('.search-input');
        if (busca) {
          event.preventDefault();
          busca.focus();
        }
      }
    });
  });
})();
