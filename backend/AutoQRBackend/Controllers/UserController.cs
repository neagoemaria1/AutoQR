using AutoQRBackend.Services;
using AutoQRBackend.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace AutoQRBackend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UserController : ControllerBase
    {
        private readonly UserService _userService;

        public UserController(UserService userService)
        {
            _userService = userService;
        }

   
        [HttpPost("getUserByQrCode")]
        public async Task<IActionResult> GetUserByQrCode([FromBody] GetUserByQrCodeRequest request)
        {
            var user = await _userService.GetUserByQrCodeAsync(request.QrCode);

            if (user == null)
            {
                return NotFound(new { message = "User not found" });
            }

            return Ok(user);
        }
    }
}
