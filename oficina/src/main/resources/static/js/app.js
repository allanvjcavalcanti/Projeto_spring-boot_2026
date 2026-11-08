/*
 * Comportamentos gerais das páginas:
 *  - formulários com data-confirmar pedem confirmação antes de enviar;
 *  - depois do envio os botões ficam desativados, para um clique duplo
 *    não criar o mesmo registro duas vezes.
 */
(function () {
    'use strict';

    document.addEventListener('submit', function (evento) {
        var form = evento.target;
        var pergunta = form.getAttribute('data-confirmar');

        if (pergunta && !window.confirm(pergunta)) {
            evento.preventDefault();
            return;
        }

        var botoes = form.querySelectorAll('button[type="submit"]');
        for (var i = 0; i < botoes.length; i++) {
            botoes[i].disabled = true;
        }
    });

    // ao voltar pelo botão "Voltar" do navegador a página pode vir do cache com os botões travados
    window.addEventListener('pageshow', function (evento) {
        if (evento.persisted) {
            var botoes = document.querySelectorAll('button[type="submit"]');
            for (var i = 0; i < botoes.length; i++) {
                botoes[i].disabled = false;
            }
        }
    });
})();
