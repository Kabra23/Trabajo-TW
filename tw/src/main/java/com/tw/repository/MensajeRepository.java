package com.tw.repository;

import com.tw.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /** Bandeja de entrada: mensajes recibidos no eliminados */
    @Query("SELECT m FROM Mensaje m WHERE m.destinatario.id = :uid AND m.eliminadoDestinatario = false ORDER BY m.fecha DESC")
    List<Mensaje> findBandejaEntrada(@Param("uid") Long usuarioId);

    /** Mensajes enviados no eliminados */
    @Query("SELECT m FROM Mensaje m WHERE m.remitente.id = :uid AND m.eliminadoRemitente = false ORDER BY m.fecha DESC")
    List<Mensaje> findEnviados(@Param("uid") Long usuarioId);

    /** Numero de mensajes no leidos */
    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.destinatario.id = :uid AND m.leido = false AND m.eliminadoDestinatario = false")
    long countNoLeidos(@Param("uid") Long usuarioId);
}
