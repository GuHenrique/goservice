package com.soulcode.goserviceapp.controller;

import com.soulcode.goserviceapp.domain.*;
import com.soulcode.goserviceapp.service.*;
import com.soulcode.goserviceapp.service.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping(value = "/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private PrestadorService prestadorService;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EnderecoService enderecoService;


    @GetMapping(value = "/dados")
    public ModelAndView dados(Authentication authentication) {
        ModelAndView mv = new ModelAndView("dadosCliente");
        try {
            Cliente cliente = clienteService.findAuthenticated(authentication);
            mv.addObject("cliente", cliente);
        } catch (UsuarioNaoAutenticadoException | UsuarioNaoEncontradoException ex) {
            mv.addObject("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            mv.addObject("errorMessage", "Erro ao buscar dados do cliente.");
        }
        return mv;
    }

    @PostMapping(value = "/dados")
    public String alterarDados(Cliente cliente, RedirectAttributes attributes) {
        try {
            clienteService.update(cliente);
            attributes.addFlashAttribute("successMessage", "Dados alterados.");
        } catch (UsuarioNaoEncontradoException ex) {
            attributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            attributes.addFlashAttribute("errorMessage", "Erro ao alterar dados cadastrais.");
        }
        Boolean emailModified = usuarioService.updatedEmail(cliente);
        if (emailModified == true) {
            return "redirect:/auth/login";
        }
        return "redirect:/cliente/dados";
    }

    @GetMapping(value = "/endereco")
    public ModelAndView endereco(Authentication authentication) {
        ModelAndView mv = new ModelAndView("enderecoCliente");
        try {
            Cliente cliente = clienteService.findAuthenticated(authentication);
            mv.addObject("estados", enderecoService.getEstados());
            mv.addObject("endereco", cliente.getEndereco());
            mv.addObject("cliente", cliente);
        } catch (EnderecoNaoEncontradoException ex) {
            mv.addObject("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            mv.addObject("errorMessage", "Erro ao buscar dados do endereço.");
        }
        return mv;
    }

    @PostMapping(value = "/endereco")
    public String alterarEndereco(Endereco endereco, RedirectAttributes attributes) {
        try {
            enderecoService.update(endereco);
            attributes.addFlashAttribute("successMessage", "Endereço alterado com sucesso");
        } catch(UsuarioNaoAutenticadoException | UsuarioNaoEncontradoException ex) {
            attributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            attributes.addFlashAttribute("errorMessage", "Erro ao alterar o endereço");
        }
        return "redirect:/cliente/endereco";
    }


    @GetMapping(value = "/agendar")
    public ModelAndView agendar(@RequestParam(name = "especialidade", required = false) Long servicoId) {
        ModelAndView mv = new ModelAndView("agendarServico");
        try {
            List<Servico> servicos = servicoService.findAll();
            mv.addObject("servicos", servicos);
            if (servicoId != null) {
                List<Prestador> prestadores = prestadorService.findByServicoId(servicoId);
                mv.addObject("prestadores", prestadores);
                mv.addObject("servicoId", servicoId);
            }
        } catch (Exception ex) {
            mv.addObject("errorMessage", "Erro ao buscar dados de serviços.");
        }
        return mv;
    }

    @PostMapping(value = "/agendar")
    public String criarAgendamento(
            @RequestParam(name = "servicoId") Long servicoId,
            @RequestParam(name = "prestadorId") Long prestadorId,
            @RequestParam(name = "data") LocalDate data,
            @RequestParam(name = "hora") LocalTime hora,
            Authentication authentication,
            RedirectAttributes attributes) {
        try {
            agendamentoService.create(authentication, servicoId, prestadorId, data, hora);
            attributes.addFlashAttribute("successMessage", "Agendamento realizado com sucesso. Aguardando confirmação.");
        } catch (UsuarioNaoAutenticadoException | UsuarioNaoEncontradoException | ServicoNaoEncontradoException ex) {
            attributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            attributes.addFlashAttribute("errorMessage", "Erro ao finalizar agendamento.");
        }
        return "redirect:/cliente/historico";
    }

    @GetMapping(value = "/historico")
    public ModelAndView historico(Authentication authentication) {
        ModelAndView mv = new ModelAndView("historicoCliente");
        try {
            List<Agendamento> agendamentos = agendamentoService.findByCliente(authentication);
            mv.addObject("agendamentos", agendamentos);
        } catch (UsuarioNaoAutenticadoException | UsuarioNaoEncontradoException ex) {
            mv.addObject("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            mv.addObject("errorMessage", "Erro ao carregar dados de agendamentos.");
        }
        return mv;
    }

    @PostMapping(value = "/historico/cancelar")
    public String cancelarAgendamento(
            @RequestParam(name = "agendamentoId") Long agendamentoId,
            Authentication authentication,
            RedirectAttributes attributes) {
        try {
            agendamentoService.cancelAgendaCliente(authentication, agendamentoId);
            attributes.addFlashAttribute("successMessage", "Agendamento cancelado.");
        } catch (UsuarioNaoAutenticadoException | UsuarioNaoEncontradoException |
                 AgendamentoNaoEncontradoException | StatusAgendamentoImutavelException ex) {
            attributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            attributes.addFlashAttribute("errorMessage", "Erro ao cancelar agendamento.");
        }
        return "redirect:/cliente/historico";
    }

    @PostMapping(value = "/historico/concluir")
    public String concluirAgendamento(
            @RequestParam(name = "agendamentoId") Long agendamentoId,
            Authentication authentication,
            RedirectAttributes attributes) {
        try {
            agendamentoService.completeAgenda(authentication, agendamentoId);
            attributes.addFlashAttribute("successMessage", "Agendamento concluído.");
        } catch (UsuarioNaoAutenticadoException | UsuarioNaoEncontradoException |
                 AgendamentoNaoEncontradoException | StatusAgendamentoImutavelException ex) {
            attributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            attributes.addFlashAttribute("errorMessage", "Erro ao concluir agendamento.");
        }
        return "redirect:/cliente/historico";
    }

    @PostMapping(value = "/historico/busca")
    public String findAgendamentoByData(@RequestParam(required = false) LocalDate dataInicio, @RequestParam(required = false) LocalDate dataFim, Authentication authentication, RedirectAttributes attributes) {
        try {
            List<Agendamento> agendamentos = clienteService.findByDataAgendamento(authentication, dataInicio, dataFim);
            attributes.addFlashAttribute("agendamentosPorData", agendamentos);
        } catch (UsuarioNaoEncontradoException | UsuarioNaoAutenticadoException e) {
            attributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("errorMessage", "Insira uma data válida");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao exibir dados");
        }
        return "redirect:/cliente/historico";
    }

}
