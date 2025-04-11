using AutoQRBackend.Services;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);
builder.WebHost.UseUrls("https://localhost:5001");

builder.Services.AddControllers();
builder.Services.AddSingleton<AuthService>();

builder.Services.AddCors(options =>
{
	options.AddPolicy("AllowAll", builder =>
	{
		builder.AllowAnyOrigin()
				 .AllowAnyHeader()
				 .AllowAnyMethod();
	});
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
	c.SwaggerDoc("v1", new OpenApiInfo
	{
		Title = "AutoQR API",
		Version = "v1"
	});
});


Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", "autoqr-4e823-firebase-adminsdk-fbsvc-bf73ebb448.json");

var app = builder.Build();

app.UseCors("AllowAll");

if (app.Environment.IsDevelopment())
{
	app.UseSwagger();
	app.UseSwaggerUI(c =>
	{
		c.SwaggerEndpoint("/swagger/v1/swagger.json", "AutoQR API v1");
	});
}
app.MapGet("/", () => Results.Redirect("/swagger"));

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();
