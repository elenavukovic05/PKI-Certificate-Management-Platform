import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCertificatesComponent } from './create-certificates.component';

describe('CreateCertificatesComponent', () => {
  let component: CreateCertificatesComponent;
  let fixture: ComponentFixture<CreateCertificatesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCertificatesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateCertificatesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
