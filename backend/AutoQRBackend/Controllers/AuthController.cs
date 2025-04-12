using AutoQRBackend.DTOs;
using AutoQRBackend.Services;
using Microsoft.AspNetCore.Mvc;

namespace AutoQRBackend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
	private readonly AuthService _authService;
	private readonly UserService _userService;

	public AuthController(AuthService authService, UserService userService)
	{
		_authService = authService;
		_userService = userService;
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

	[HttpGet("me")]
	public async Task<IActionResult> GetCurrentUser()
	{
		var authHeader = Request.Headers["Authorization"].ToString();
		if (!authHeader.StartsWith("Bearer "))
			return Unauthorized();

		var token = authHeader.Substring("Bearer ".Length).Trim();

		var user = await _authService.GetUserFromTokenAsync(token);
		if (user == null)
			return NotFound();

		var response = new UserProfileResponse
		{
			Email = user.Email,
			Username = user.Username,
			QrCode = user.QrCode,
			ProfileImageUrl = user.ProfileImageUrl,
			DeviceToken = user.DeviceToken
		};

		return Ok(response);
	}




}
