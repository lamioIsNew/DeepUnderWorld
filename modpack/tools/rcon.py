# -*- coding: utf-8 -*-
"""Tiny RCON client used to smoke-test the underworld on a dedicated server."""

from __future__ import annotations

import socket
import struct
import sys


class Rcon:
    def __init__(self, host: str, port: int, password: str, timeout: float = 30.0):
        self.sock = socket.create_connection((host, port), timeout=timeout)
        self.next_id = 1
        self._send(3, password)
        _, kind, _ = self._recv()
        if kind != 2:
            raise RuntimeError("RCON authentication failed")

    def _send(self, kind: int, body: str) -> int:
        request_id = self.next_id
        self.next_id += 1
        payload = body.encode("utf-8") + b"\x00\x00"
        packet = struct.pack("<iii", request_id, kind, len(payload) + 8) + payload
        self.sock.sendall(packet)
        return request_id

    def _recv(self):
        raw_length = self._read_exact(4)
        length = struct.unpack("<i", raw_length)[0]
        data = self._read_exact(length)
        request_id, kind = struct.unpack("<ii", data[:8])
        body = data[8:-2].decode("utf-8", errors="replace")
        return request_id, kind, body

    def _read_exact(self, count: int) -> bytes:
        chunks = bytearray()
        while len(chunks) < count:
            chunk = self.sock.recv(count - len(chunks))
            if not chunk:
                raise ConnectionError("RCON connection closed")
            chunks.extend(chunk)
        return bytes(chunks)

    def command(self, command: str) -> str:
        self._send(2, command)
        _, _, body = self._recv()
        return body

    def close(self) -> None:
        self.sock.close()


def main() -> None:
    rcon = Rcon("127.0.0.1", 25575, "wcnmtest")
    try:
        for command in sys.argv[1:]:
            print(f"> {command}")
            print(rcon.command(command))
    finally:
        rcon.close()


if __name__ == "__main__":
    main()
