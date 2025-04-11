using AutoQRBackend.Services;
using Microsoft.AspNetCore.Mvc;

namespace AutoQRBackend.Controllers
{
    [ApiController]
    [Route("api/notification")]
    public class NotificationController : ControllerBase
    {
        private readonly UserService _userService;
        private readonly AuthService _authService;
        public NotificationController(UserService userService, AuthService authService)
        {
            _userService = userService;
            _authService = authService;
        }
        [HttpPost("token")]
        public async Task<IActionResult> UpdateDeviceToken([FromHeader] string Authorization, [FromBody] string deviceToken)
        {
            var token = Authorization.Replace("Bearer ", "").Trim();
            var user = await _authService.GetUserFromTokenAsync(token);
            if (user == null) return NotFound(new { message = "User not found." });
            user.DeviceToken = deviceToken;
            await _userService.UpdateUserAsync(user);
            return Ok(new { message = "Device token updated successfully!" });
        }
    }
}
