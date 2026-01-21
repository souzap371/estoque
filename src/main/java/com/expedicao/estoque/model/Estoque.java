// package com.expedicao.estoque.model;

// import jakarta.persistence.*;

// @Entity
// @Table(
//     uniqueConstraints = @UniqueConstraint(columnNames = {"produto_id", "filial"})
// )
// public class Estoque {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne
//     @JoinColumn(nullable = false)
//     private Produto produto;

//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false)
//     private Filial filial;

//     @Column(nullable = false)
//     private Integer quantidadeAtual = 0;

//     public Long getId() {
//         return id;
//     }

//     public Produto getProduto() {
//         return produto;
//     }

//     public void setProduto(Produto produto) {
//         this.produto = produto;
//     }

//     public Filial getFilial() {
//         return filial;
//     }

//     public void setFilial(Filial filial) {
//         this.filial = filial;
//     }

//     public Integer getQuantidadeAtual() {
//         return quantidadeAtual;
//     }

//     public void setQuantidadeAtual(Integer quantidadeAtual) {
//         this.quantidadeAtual = quantidadeAtual;
//     }
// }



package com.expedicao.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "estoque",
    uniqueConstraints = @UniqueConstraint(columnNames = {"produto_id", "filial"})
)
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Filial filial;

    @Column(nullable = false)
    private Integer quantidadeAtual = 0;

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }

    public Integer getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(Integer quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }
}
