using AutoQRBackend.DTOs;
using AutoQRBackend.Services;
using Microsoft.AspNetCore.Mvc;

namespace AutoQRBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
	private readonly AuthService _authService;

	public AuthController(AuthService authService)
	{
		_authService = authService;
	}

	[HttpPost("register")]
	public async Task<IActionResult> Register([FromBody] RegisterRequest req)
	{
		var result = await _authService.RegisterAsync(req);

		if (result == null)
			return Ok(new { message = "Account successfully created!" });

		return BadRequest(new { error = result });
	}

	[HttpPost("login")]
	public async Task<IActionResult> Login([FromBody] LoginRequest req)
	{
		var token = await _authService.LoginAsync(req);

		if (token == null)
			return Unauthorized(new { error = "Invalid credentials" });

		return Ok(new { token });
	}
}
